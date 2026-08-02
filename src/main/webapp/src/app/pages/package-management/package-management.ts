import { ChangeDetectorRef, Component, OnInit, ViewChild, TemplateRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { MatIconModule } from '@angular/material/icon';
import { MatDialogModule, MatDialog, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { AppAvatar, AppButton, DataTable, PageHeader } from '../../components';

@Component({
  selector: 'app-package-management',
  imports: [CommonModule, FormsModule,
    MatIconModule, MatDialogModule,
    MatFormFieldModule, MatInputModule, MatPaginatorModule,
    AppAvatar, AppButton, DataTable, PageHeader],
  templateUrl: './package-management.html',
})
export class PackageManagement implements OnInit {
  packages: any[] = [];
  form = { name: '', maxEmployeeSeats: 1, maxCarListings: 1 };
  editingId: number | null = null;
  dialogRef: MatDialogRef<any> | null = null;
  totalElements = 0;
  pageIndex = 0;
  pageSize = 20;

  @ViewChild('dialogTemplate') dialogTemplate!: TemplateRef<any>;

  constructor(
    private http: HttpClient,
    private dialog: MatDialog,
    private cd: ChangeDetectorRef
  ) {}

  ngOnInit() { this.load(); }

  load() {
    this.http.get<any>('/api/v1/packages', {
      params: { page: this.pageIndex, size: this.pageSize, sort: 'id,asc' }
    }).subscribe(d => {
      this.packages = d.content ?? [];
      this.totalElements = d.totalElements ?? this.packages.length;
      this.cd.detectChanges();
    });
  }

  pageChanged(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.load();
  }

  openCreateDialog() {
    this.form = { name: '', maxEmployeeSeats: 1, maxCarListings: 1 };
    this.editingId = null;
    this.dialogRef = this.dialog.open(this.dialogTemplate);
  }

  save() {
    const body = this.form;
    const req = this.editingId
      ? this.http.put(`/api/v1/packages/${this.editingId}`, body)
      : this.http.post('/api/v1/packages', body);
    req.subscribe(() => { this.load(); this.dialogRef?.close(); });
  }

  deletePackage(id: number) {
    if (confirm('Delete this package?')) {
      this.http.delete(`/api/v1/packages/${id}`).subscribe(() => this.load());
    }
  }
}
