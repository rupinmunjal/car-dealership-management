import { Component, OnInit, ViewChild, TemplateRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { MatTableModule } from '@angular/material/table';
import { MatCardModule } from '@angular/material/card';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatDialogModule, MatDialog, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatChipsModule } from '@angular/material/chips';

@Component({
  selector: 'app-package-management',
  imports: [CommonModule, FormsModule,
    MatTableModule, MatCardModule, MatToolbarModule, MatButtonModule,
    MatIconModule, MatTooltipModule, MatDialogModule,
    MatFormFieldModule, MatInputModule, MatChipsModule],
  templateUrl: './package-management.html',
  styleUrl: './package-management.css',
})
export class PackageManagement implements OnInit {
  packages: any[] = [];
  columns = ['name', 'seats', 'listings', 'actions'];
  form = { name: '', maxEmployeeSeats: 1, maxCarListings: 1 };
  editingId: number | null = null;
  dialogRef: MatDialogRef<any> | null = null;
  @ViewChild('dialogTemplate') dialogTemplate!: TemplateRef<any>;

  constructor(private http: HttpClient, private dialog: MatDialog) {}

  ngOnInit() { this.load(); }

  load() { this.http.get<any[]>('/api/v1/packages').subscribe(d => this.packages = d); }

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
